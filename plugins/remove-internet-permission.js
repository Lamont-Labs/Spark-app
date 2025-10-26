const {
  withAndroidManifest,
  withDangerousMod,
  AndroidConfig
} = require("@expo/config-plugins");
const fs = require("fs");
const path = require("path");

const XML_REL_PATH = "app/src/main/res/xml";
const XML_FILE = "network_security_config.xml";
const XML_CONTENT = `<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
  <base-config cleartextTrafficPermitted="false">
    <trust-anchors>
      <!-- No user or system CAs: deny all external connections -->
    </trust-anchors>
  </base-config>
</network-security-config>
`;

function removeInternetPermission(androidManifest) {
  AndroidConfig.Manifest.removeUsesPermission(
    androidManifest,
    "android.permission.INTERNET"
  );
  return androidManifest;
}

const withDenyAllXml = (config) =>
  withDangerousMod(config, [
    "android",
    async (cfg) => {
      const projDir = cfg.modRequest.platformProjectRoot;
      const xmlDir = path.join(projDir, XML_REL_PATH);
      const xmlPath = path.join(xmlDir, XML_FILE);
      fs.mkdirSync(xmlDir, { recursive: true });
      fs.writeFileSync(xmlPath, XML_CONTENT, "utf8");
      return cfg;
    }
  ]);

const withNetworkConfigRef = (config) =>
  withAndroidManifest(config, (cfg) => {
    const manifest = cfg.modResults.manifest;
    const app = manifest.application?.[0];
    if (app) {
      app.$["android:networkSecurityConfig"] = "@xml/network_security_config";
    }
    cfg.modResults.manifest = removeInternetPermission(cfg.modResults.manifest);
    return cfg;
  });

module.exports = function withRemoveInternetPermission(config) {
  config = withDenyAllXml(config);
  config = withNetworkConfigRef(config);
  return config;
};
