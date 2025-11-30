import { Configuration, UsersApi, WishesApi, AuthApi } from "../api";

let authToken: string | null = null;

const apiConfig = new Configuration({
  basePath: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api",
  accessToken: async () => authToken ?? "",
});

export const setAuthToken = (token: string | null) => {
  authToken = token;
};

export const authApi = new AuthApi(apiConfig);
export const usersApi = new UsersApi(apiConfig);
export const wishesApi = new WishesApi(apiConfig);