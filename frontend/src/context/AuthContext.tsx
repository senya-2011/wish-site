import { createContext, useContext, useEffect, useState, useCallback } from "react";
import type { ReactNode } from "react";
import type { LoginRequest, RegisterRequest, LoginResponse, UserResponse } from "../api";
import { authApi, setAuthToken } from "../lib/api-client";

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

