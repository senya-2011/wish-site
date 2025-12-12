import { useCallback, useEffect, useLayoutEffect, useState } from "react";
import type { ReactNode } from "react";
import type { LoginRequest, RegisterRequest, LoginResponse, UserResponse } from "../api";
import { authApi, setAuthToken, setUnauthorizedHandler } from "../lib/api-client";
import { AuthContext } from "./AuthContext";

const TOKEN_STORAGE_KEY = "dabwish.accessToken";
const USER_STORAGE_KEY = "dabwish.user";

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

const readInitialToken = (): string | null => {
  const stored = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (!stored) {
    return null;
  }
  if (isTokenExpired(stored)) {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    return null;
  }
  return stored;
};

const readInitialUser = (): UserResponse | null => {
  const raw = localStorage.getItem(USER_STORAGE_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as UserResponse;
  } catch {
    localStorage.removeItem(USER_STORAGE_KEY);
    return null;
  }
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(readInitialToken);
  const [user, setUser] = useState<UserResponse | null>(readInitialUser);

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

  const refreshUser = useCallback(async () => {
    if (token && user?.user_id) {
      try {
        const { usersApi } = await import("../lib/api-client");
        const response = await usersApi.getUserById(user.user_id);
        setUser(response.data);
      } catch (error) {
        console.error("Failed to refresh user:", error);
      }
    }
  }, [token, user?.user_id]);

  useLayoutEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  return (
    <AuthContext.Provider value={{ token, user, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
};

