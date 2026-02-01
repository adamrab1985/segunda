import { NextResponse } from "next/server";
import { prisma } from "@/lib/db";

export const runtime = "nodejs";

export async function GET() {
  try {
    if (!process.env.DATABASE_URL) {
      return NextResponse.json({
        ok: false,
        error: "DATABASE_URL is not set in Vercel",
      });
    }
    await prisma.$queryRaw`SELECT 1`;
    const count = await prisma.xReportSnapshot.count();
    const secretConfigured = Boolean(process.env.X_REPORT_SECRET);
    return NextResponse.json({
      ok: true,
      message: "Database connected",
      tableExists: true,
      reportCount: count,
      secretConfigured,
    });
  } catch (e: unknown) {
    const message = e instanceof Error ? e.message : String(e);
    console.error("Health check error:", e);
    return NextResponse.json(
      {
        ok: false,
        error: message,
        hint: message.includes("does not exist")
          ? "Run: cd Segunda && npx prisma db push"
          : "Check DATABASE_URL in Vercel and add &pgbouncer=true if using Neon pooler",
      },
      { status: 500 }
    );
  }
}
