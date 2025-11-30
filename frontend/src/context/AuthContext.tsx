import { createContext, useContext, useEffect, useLayoutEffect, useState, useCallback } from "react";
import type { ReactNode } from "react";
import type { LoginRequest, RegisterRequest, LoginResponse, UserResponse } from "../api";
import { authApi, setAuthToken, setUnauthorizedHandler } from "../lib/api-client";

type AuthContextValue = {
  token: string | null;
  user: UserResponse | null;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => void;
};

const TOKEN_STORAGE_KEY = "dabwish.accessToken";
const USER_STORAGE_KEY = "dabwish.user";

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const decodeBase64Url = (value: string): string => {
  const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
  return atob(padded);
};

const isTokenExpired = (token: string): boolean => {
  try {
    const [, payload] = token.split(".");
    if (!payload) {
      return true;
    }
    const parsed = JSON.parse(decodeBase64Url(payload)) as { exp?: number };
    if (!parsed.exp) {
      return false;
    }
    return parsed.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_STORAGE_KEY));
  const [user, setUser] = useState<UserResponse | null>(() => {
    const raw = localStorage.getItem(USER_STORAGE_KEY);
    return raw ? (JSON.parse(raw) as UserResponse) : null;
  });

  useEffect(() => {
    setAuthToken(token);
    if (token) {
      localStorage.setItem(TOKEN_STORAGE_KEY, token);
    } else {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    }
  }, [token]);

  useEffect(() => {
    if (user) {
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    } else {
      localStorage.removeItem(USER_STORAGE_KEY);
    }
  }, [user]);

  const handleAuthSuccess = (data: LoginResponse) => {
    setToken(data.access_token);
    setUser(data.user ?? null);
  };

  const login = useCallback(
    async (payload: LoginRequest) => {
      const { data } = await authApi.login(payload);
      handleAuthSuccess(data);
    },
    [],
  );

  const register = useCallback(
    async (payload: RegisterRequest) => {
      const { data } = await authApi.register(payload);
      handleAuthSuccess(data);
    },
    [],
  );

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  useLayoutEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  useLayoutEffect(() => {
    if (token && isTokenExpired(token)) {
      logout();
    }
  }, [token, logout]);

  return (
    <AuthContext.Provider value={{ token, user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextValue => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
};

