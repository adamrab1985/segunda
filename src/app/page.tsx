export default function Home() {
  return (
    <main style={{ padding: "2rem", fontFamily: "system-ui" }}>
      <h1>Segunda X Report</h1>
      <p>Backend is running. The Android app uses <code>GET /api/xreport</code>.</p>
      <p><a href="/api/xreport/health">Health check</a></p>
    </main>
  );
}
