import React from "react";
import { SafeAreaView, StatusBar } from "react-native";
import Today from "./screens/Today";
import { installNetworkGuards } from "./lib/no-net";

// enforce offline-only runtime
installNetworkGuards();

export default function App() {
  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: "#0B1020" }}>
      <StatusBar barStyle="light-content" />
      <Today />
    </SafeAreaView>
  );
}
