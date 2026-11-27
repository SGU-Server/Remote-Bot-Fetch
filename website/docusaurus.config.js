// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import { themes as prismThemes } from "prism-react-renderer";

const isCloudflarePages = process.env.CF_PAGES === "1";
const isMainBranch = process.env.CF_PAGES_BRANCH === "main";
let url, baseUrl, commitUrl, commitShaShort;
if (isCloudflarePages) {
  url = "https://rbf.sgu-server.com";
  baseUrl = "/";
  const commitSha = process.env.CF_PAGES_COMMIT_SHA;
  if (commitSha) {
    commitUrl = `https://github.com/SGU-Server/Remote-Bot-Fetch/commit/${commitSha}`;
    commitShaShort = commitSha.substring(0, 7);
  }
} else {
  url = "https://rbf.sgu-server.com";
  baseUrl = "/";
}

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: "Remote Bot Fetch",
  tagline:
    "A practical Carpet fake player storage mod that assists survival play.",
  favicon: "img/icon.png",
  headTags: [
    {
      tagName: "meta",
      attributes: {
        name: "algolia-site-verification",
        content: "919577A03A514394",
      },
    },
  ],

  // Set the production url of your site here
  url: url,
  // Set the /<baseUrl>/ pathname under which your site is served
  baseUrl: baseUrl,

  trailingSlash: false,

  onBrokenLinks: "throw",
  markdown: {
    hooks: {
      onBrokenMarkdownLinks: "warn",
    },
  },

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "en",
    locales: ["en", "zh-Hans"],
  },

  presets: [
    [
      "classic",
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        blog: false,
        docs: {
          sidebarPath: "./sidebars.js",
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
            "https://github.com/SGU-Server/Remote-Bot-Fetch/tree/main/website/",
        },
        theme: {
          customCss: "./src/css/custom.css",
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: "Remote Bot Fetch",
        logo: {
          alt: "Remote Bot Fetch Icon",
          src: "img/icon.png",
        },
        items: [
          {
            type: "docSidebar",
            sidebarId: "tutorialSidebar",
            position: "left",
            label: "Documents",
          },
          {
            to: "eol",
            position: "left",
            label: "EOL",
          },
          {
            type: "search",
            position: "right",
          },
          {
            type: "localeDropdown",
            position: "right",
          },
          {
            href: "https://github.com/SGU-Server/Remote-Bot-Fetch",
            className: "header-github-link",
            "aria-label": "GitHub repository",
            position: "right",
          },
        ],
      },
      footer: {
        style: "light",
        links: [
          {
            title: "Download",
            items: [
              {
                label: "GitHub Releases",
                href: "https://github.com/SGU-Server/Remote-Bot-Fetch/releases",
              },
              {
                label: "CurseForge",
                href: "https://www.curseforge.com/minecraft/mc-mods/remote-bot-fetch/files",
              },
              {
                label: "Modrinth",
                href: "https://modrinth.com/mod/remote-bot-fetch/versions",
              },
            ],
          },
          {
            title: "Contact",
            items: [
              {
                label: "GitHub Repository",
                href: "https://github.com/SGU-Server/Remote-Bot-Fetch",
              },
            ],
          },
          {
            title: "Related Projects",
            items: [
              {
                label: "Remote Bot Fetch Server",
                href: "https://github.com/SGU-Server/Remote-Bot-Fetch-Server",
              },
              {
                label: "Carpet LMS Addition",
                href: "https://github.com/Citrus-Union/Carpet-LMS-Addition",
              },
              {
                label: "antideath-survival-assistant",
                href: "https://github.com/jfglzs/Antideath-survival-assistant",
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Remote Bot Fetch. Built with Docusaurus.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
      // https://docusaurus.io/docs/search#using-algolia-docsearch
      algolia: {
        appId: "5IXFHKZ83W",
        apiKey: "d9cb5b103dedaac0a2e0814f7b6019be",
        indexName: "remote-bot-fetch",
        contextualSearch: true,
      },
      announcementBar:
        isCloudflarePages && !isMainBranch && commitUrl
          ? {
              id: "test_environment",
              content: `⚠️ This is the test environment of the Remote Bot Fetch website. Built for commit <a href="${commitUrl}">${commitShaShort}</a>. ⚠️`,
              backgroundColor: "#ffdf76",
              textColor: "#091E42",
              isCloseable: false,
            }
          : undefined,
    }),
  plugins: [
    // Generate robots.txt alongside the sitemap during production builds.
    "./plugins/robots-txt",
    [
      // https://github.com/dipakparmar/docusaurus-plugin-umami
      "@dipakparmar/docusaurus-plugin-umami",
      /** @type {import('@dipakparmar/docusaurus-plugin-umami').Options} */
      ({
        websiteID: "6e13b48c-efe7-4ae9-bad9-05edc9b7c27e", // Required
        analyticsDomain: "umami.sgu-server.com", // Required
        scriptName: "script.js", // Optional
        dataDomains: "rbf.sgu-server.com", // comma separated list of domains, *Recommended*
        enableRecorder: true,
      }),
    ],
  ],
};

export default config;
