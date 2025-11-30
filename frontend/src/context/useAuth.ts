import { useContext } from "react";
import type { AuthContextValue } from "./types";
import { AuthContext } from "./AuthContext";

export const useAuth = (): AuthContextValue => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
};

