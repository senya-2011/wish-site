import { Configuration, UsersApi, WishesApi, AuthApi, FilesApi } from "../api";
import axios from "axios";

const basePath = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";
const apiConfig = new Configuration();

const axiosInstance = axios.create({
  baseURL: basePath,
});
let unauthorizedHandler: (() => void) | null = null;

export const setAuthToken = (token: string | null) => {
  if (token) {
    axiosInstance.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    delete axiosInstance.defaults.headers.common.Authorization;
  }
};

export const setUnauthorizedHandler = (handler: (() => void) | null) => {
  unauthorizedHandler = handler;
};

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    if (status === 401 || status === 403) {
      unauthorizedHandler?.();
    }
    return Promise.reject(error);
  },
);

export const authApi = new AuthApi(apiConfig, basePath, axiosInstance);
export const usersApi = new UsersApi(apiConfig, basePath, axiosInstance);
export const wishesApi = new WishesApi(apiConfig, basePath, axiosInstance);
export const filesApi = new FilesApi(apiConfig, basePath, axiosInstance);