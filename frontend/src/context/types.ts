import type { LoginRequest, RegisterRequest, UserResponse } from "../api";

export type AuthContextValue = {
  token: string | null;
  user: UserResponse | null;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshUser?: () => Promise<void>;
};

