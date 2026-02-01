"""
Caspit Light X Report: scrapes daily/monthly totals for Segunda stores,
sends to Telegram and POSTs to your API for the Android app.
Loads X_REPORT_API_URL and X_REPORT_SECRET from Segunda/.env
"""
import os
import time
from pathlib import Path

from dotenv import load_dotenv
load_dotenv(Path(__file__).resolve().parent.parent / ".env")

import requests
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

VAT_RATE = 1.18

TELEGRAM_TOKEN = os.environ.get("TELEGRAM_TOKEN", "")
TELEGRAM_USER_IDS = os.environ.get("TELEGRAM_USER_IDS", "").split(",")

ACCOUNTS = [
    {"name": "Pop Up", "username": "3722741BO", "password": "2741BO987"},
    {"name": "Main", "username": "3717911BO", "password": "7911BO987"},
    {"name": "Rehovot", "username": "3729172BO", "password": "CTchaos03!"},
]

CHROME_DRIVER_PATH = os.environ.get(
    "CHROME_DRIVER_PATH",
    "/Users/adamrab/chrome-tools/chromedriver-mac-arm64/chromedriver",
)

LOGIN_URL = "https://caspitlight.valu.co.il/bo#/login"
XREPORT_URL = "https://caspitlight.valu.co.il/bo#/xreports"
SALES_URL = "https://caspitlight.valu.co.il/bo#/sales"

X_REPORT_API_URL = os.environ.get("X_REPORT_API_URL", "").rstrip("/")
X_REPORT_SECRET = os.environ.get("X_REPORT_SECRET", "")


def send_telegram_message(message: str):
    if not TELEGRAM_TOKEN or not any(TELEGRAM_USER_IDS):
        return
    url = f"https://api.telegram.org/bot{TELEGRAM_TOKEN}/sendMessage"
    for user_id in TELEGRAM_USER_IDS:
        user_id = user_id.strip()
        if not user_id:
            continue
        try:
            requests.post(url, data={"chat_id": user_id, "text": message, "parse_mode": "Markdown"}, timeout=10)
        except Exception:
            pass


def post_report_to_api(report_date: str, report_time: str, stores: list, totals: dict):
    if not X_REPORT_API_URL or not X_REPORT_SECRET:
        print("(API POST skipped: X_REPORT_API_URL or X_REPORT_SECRET not set in .env)")
        return
    url = f"{X_REPORT_API_URL}/api/xreport"
    print(f"Posting to: {url}")
    headers = {"Content-Type": "application/json", "x-report-secret": X_REPORT_SECRET}
    try:
        r = requests.post(url, json={"reportDate": report_date, "reportTime": report_time, "stores": stores, "totals": totals}, headers=headers, timeout=15)
        if r.status_code in (200, 201):
            print("Report posted to API successfully.")
        else:
            print(f"API POST failed: {r.status_code} - {r.text[:200]}")
    except Exception as e:
        print(f"API POST error: {e}")


def get_totals(account):
    options = webdriver.ChromeOptions()
    options.add_argument("--headless")
    options.binary_location = (
        "/Users/adamrab/chrome-tools/chrome-mac-arm64/"
        "Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing"
    )
    driver = webdriver.Chrome(service=Service(CHROME_DRIVER_PATH), options=options)
    try:
        driver.get(LOGIN_URL)
        wait = WebDriverWait(driver, 10)
        wait.until(EC.presence_of_element_located((By.ID, "username"))).send_keys(account["username"])
        wait.until(EC.presence_of_element_located((By.ID, "password"))).send_keys(account["password"])
        wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(), 'התחבר')]"))).click()
        time.sleep(5)
        driver.get(XREPORT_URL)
        wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "div.box-value.dir-ltr")))
        daily_total = driver.find_element(By.CSS_SELECTOR, "div.box-value.dir-ltr").text.strip()
        driver.get(SALES_URL)
        time.sleep(5)
        spans = driver.find_elements(By.CSS_SELECTOR, "span[dir='ltr']")
        values = [s.text.strip() for s in spans if s.text.strip()]
        monthly_total = values[1] if len(values) >= 2 else "0.00"
        return daily_total, monthly_total
    except Exception as e:
        print("Error:", e)
        return "0.00", "0.00"
    finally:
        driver.quit()


def main():
    # Uncomment next 3 lines to skip Saturday
    # if time.strftime("%A") == "Saturday":
    #     print("Saturday — skipping.")
    #     return

    now_date = time.strftime("%d/%m/%Y")
    now_time = time.strftime("%H:%M")
    messages = [f"📅 {now_date} 🕐 {now_time}"]
    total_daily = 0.0
    total_monthly = 0.0
    stores_payload = []

    for account in ACCOUNTS:
        daily, monthly = get_totals(account)
        try:
            total_daily += float(daily.replace(",", "").replace("₪", ""))
            total_monthly += float(monthly.replace(",", "").replace("₪", ""))
        except Exception:
            pass
        stores_payload.append({"name": account["name"], "daily": daily, "monthly": monthly})
        messages.append(f"\n🧾 {account['name']}\n• Today: ₪{daily}\n• Month: ₪{monthly}")

    total_daily_ex_vat = total_daily / VAT_RATE if total_daily else 0.0
    total_monthly_ex_vat = total_monthly / VAT_RATE if total_monthly else 0.0
    totals_payload = {
        "dailyInclVat": round(total_daily, 2),
        "dailyExclVat": round(total_daily_ex_vat, 2),
        "monthlyInclVat": round(total_monthly, 2),
        "monthlyExclVat": round(total_monthly_ex_vat, 2),
    }
    messages.append(
        f"\n📊 *Total*\n• Today (incl. VAT): ₪{total_daily:,.2f}\n• Month (incl. VAT): ₪{total_monthly:,.2f}"
    )

    full_message = "\n".join(messages)
    print(full_message)
    send_telegram_message(full_message)
    post_report_to_api(now_date, now_time, stores_payload, totals_payload)


if __name__ == "__main__":
    main()
