import { NextResponse } from "next/server";
import { z } from "zod";
import { prisma } from "@/lib/db";

export const runtime = "nodejs";

const storeSchema = z.object({
  name: z.string(),
  daily: z.string(),
  monthly: z.string(),
});

const totalsSchema = z.object({
  dailyInclVat: z.number(),
  dailyExclVat: z.number(),
  monthlyInclVat: z.number(),
  monthlyExclVat: z.number(),
});

const postSchema = z.object({
  reportDate: z.string(),
  reportTime: z.string(),
  stores: z.array(storeSchema),
  totals: totalsSchema,
});

export async function GET() {
  try {
    const latest = await prisma.xReportSnapshot.findFirst({
      orderBy: { createdAt: "desc" },
    });
    if (!latest) {
      return NextResponse.json(
        { error: "No report available yet." },
        { status: 404 }
      );
    }
    return NextResponse.json({
      id: latest.id,
      reportDate: latest.reportDate,
      reportTime: latest.reportTime,
      stores: latest.stores as { name: string; daily: string; monthly: string }[],
      totals: latest.totals as {
        dailyInclVat: number;
        dailyExclVat: number;
        monthlyInclVat: number;
        monthlyExclVat: number;
      },
      createdAt: latest.createdAt.toISOString(),
    });
  } catch (e: unknown) {
    const message = e instanceof Error ? e.message : String(e);
    console.error("X report GET error:", e);
    return NextResponse.json(
      { error: "Failed to fetch report.", detail: message.slice(0, 200) },
      { status: 500 }
    );
  }
}

export async function POST(req: Request) {
  const secret = process.env.X_REPORT_SECRET;
  console.log("POST: X_REPORT_SECRET exists?", !!secret);
  console.log("POST: X_REPORT_SECRET length:", secret?.length || 0);
  if (!secret) {
    return NextResponse.json(
      { error: "X_REPORT_SECRET not configured." },
      { status: 500 }
    );
  }
  const headerSecret = req.headers.get("x-report-secret");
  console.log("POST: Header secret exists?", !!headerSecret);
  console.log("POST: Header secret length:", headerSecret?.length || 0);
  console.log("POST: Secrets match?", headerSecret === secret);
  if (headerSecret !== secret) {
    return NextResponse.json({ error: "Unauthorized." }, { status: 401 });
  }

  try {
    const body = await req.json();
    const parsed = postSchema.safeParse(body);
    if (!parsed.success) {
      return NextResponse.json(
        { error: "Invalid payload", details: parsed.error.flatten() },
        { status: 400 }
      );
    }
    const { reportDate, reportTime, stores, totals } = parsed.data;
    await prisma.xReportSnapshot.create({
      data: {
        reportDate,
        reportTime,
        stores: stores as object,
        totals: totals as object,
      },
    });
    return NextResponse.json({ ok: true });
  } catch (e) {
    console.error("X report POST error:", e);
    return NextResponse.json(
      { error: "Failed to save report." },
      { status: 500 }
    );
  }
}
