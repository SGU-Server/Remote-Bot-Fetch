import { writeFile } from "node:fs/promises";
import path from "node:path";

export default function robotsTxtPlugin(context) {
  return {
    name: "robots-txt-plugin",
    async postBuild({ outDir }) {
      const content = [
        "User-agent: *",
        "Allow: /",
        `Sitemap: ${new URL("sitemap.xml", context.siteConfig.url).href}`,
        "",
      ].join("\n");

      await writeFile(path.join(outDir, "robots.txt"), content, "utf8");
    },
  };
}
