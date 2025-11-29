import { Configuration, UsersApi, WishesApi } from "../api";

const apiConfig = new Configuration({
  basePath: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api",
});

export const usersApi = new UsersApi(apiConfig);
export const wishesApi = new WishesApi(apiConfig);