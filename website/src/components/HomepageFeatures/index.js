import clsx from "clsx";
import Heading from "@theme/Heading";
import styles from "./styles.module.css";
import Translate, { translate } from "@docusaurus/Translate";

const FeatureList = [
  {
    title: translate({
      id: "homepage.feature.rich.message",
      message: "Live stock at a glance",
    }),
    Svg: require("@site/static/img/stock.svg").default,
    description: (
      <>
        <Translate id="homepage.feature.rich.description">
          Remote Bot Fetch makes stock lookups quick and easy: check the live
          item counts of every configured container at any time
        </Translate>
      </>
    ),
  },
  {
    title: translate({
      id: "homepage.feature.vanilla.message",
      message: "One jar, both sides",
    }),
    Svg: require("@site/static/img/merged-jar.svg").default,
    description: (
      <>
        <Translate id="homepage.feature.vanilla.description">
          The server and the client are merged into a single jar, so updating
          the mod is one file
        </Translate>
      </>
    ),
  },
  {
    title: translate({
      id: "homepage.feature.versions.message",
      message: "Extensive Version Support",
    }),
    Svg: require("@site/static/img/checkmark.svg").default,
    description: (
      <>
        <Translate id="homepage.feature.versions.description">
          From Minecraft 1.21 to 26.3, Remote Bot Fetch provides consistent
          support across a range of Minecraft versions
        </Translate>
      </>
    ),
  },
];

function Feature({ Svg, title, description }) {
  return (
    <div className={clsx("col col--4")}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
