import { NextResponse } from "next/server";

export const runtime = "nodejs";

const GITHUB_OWNER = "adamrab1985";
const GITHUB_REPO = "segunda";
const WORKFLOW_ID = "scrape-xreport.yml";

export async function POST(req: Request) {
  const triggerSecret = req.headers.get("x-trigger-secret");
  
  // Optional: Add a secret for triggering (you can set TRIGGER_SECRET in Vercel env vars)
  const expectedSecret = process.env.TRIGGER_SECRET;
  if (expectedSecret && triggerSecret !== expectedSecret) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const githubToken = process.env.GITHUB_TOKEN;
  if (!githubToken) {
    return NextResponse.json(
      { error: "GITHUB_TOKEN not configured in Vercel" },
      { status: 500 }
    );
  }

  try {
    const url = `https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/actions/workflows/${WORKFLOW_ID}/dispatches`;
    
    const response = await fetch(url, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${githubToken}`,
        Accept: "application/vnd.github+json",
        "Content-Type": "application/json",
        "X-GitHub-Api-Version": "2022-11-28",
      },
      body: JSON.stringify({
        ref: "main",
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("GitHub API error:", errorText);
      return NextResponse.json(
        { error: "Failed to trigger workflow", detail: errorText.slice(0, 200) },
        { status: response.status }
      );
    }

    return NextResponse.json({
      ok: true,
      message: "Workflow triggered. Check back in ~1-2 minutes.",
    });
  } catch (e: unknown) {
    const message = e instanceof Error ? e.message : String(e);
    console.error("Trigger error:", e);
    return NextResponse.json(
      { error: "Failed to trigger scraper", detail: message.slice(0, 200) },
      { status: 500 }
    );
  }
}
