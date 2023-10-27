import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Delta or Iceberg? You choose!',
    Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
    description: (
      <>
        Whitefox is a protocol and a server that enables easy data sharing
        on top of table formats.
      </>
    ),
  },
  {
    title: 'Completely compatible with Delta Sharing',
    Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
    description: (
      <>
        Whitefox was build from the ground up to be compatible with Delta Sharing protocol.
        New features are added on top of Delta Sharing protocol, so you can use
        Whitefox as a Delta Sharing server and still be compatible with Delta Sharing clients.
      </>
    ),
  },
  {
    title: 'Cloud native',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        Whitefox is a cloud native application, it can be deployed on Kubernetes
        or any other container orchestration system. It's a production ready replacement
        of Delta Sharing server.
      </>
    ),
  },
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
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
