import { useState } from "react";
import { isAxiosError } from "axios";
import { useAuth } from "../context/AuthContext";
import type { LoginRequest, RegisterRequest } from "../api";

type Mode = "login" | "register";

export const AuthPage = () => {
  const { login, register } = useAuth();
  const [mode, setMode] = useState<Mode>("login");
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      if (mode === "login") {
        await login({ name, password } as LoginRequest);
      } else {
        await register({ name, password } as RegisterRequest);
      }
    } catch (err) {
      setError(extractMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  const extractMessage = (err: unknown): string => {
    if (isAxiosError(err)) {
      return (
        (err.response?.data as { message?: string } | undefined)?.message ??
        err.response?.statusText ??
        "Не удалось выполнить запрос"
      );
    }
    if (err instanceof Error) {
      return err.message;
    }
    return "Неизвестная ошибка";
  };

  const toggleMode = () => {
    setMode((prev) => (prev === "login" ? "register" : "login"));
    setError(null);
  };

  return (
    <div className="card auth-card">
      <h2>{mode === "login" ? "Вход в систему" : "Регистрация"}</h2>
      <p className="text-muted">
        {mode === "login"
          ? "Введите имя пользователя и пароль"
          : "Создайте новый аккаунт, чтобы продолжить"}
      </p>

      <form className="form" onSubmit={handleSubmit}>
        <input
          className="form-input"
          placeholder="Имя пользователя"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          className="form-input"
          type="password"
          placeholder="Пароль"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={6}
        />
        {error && <p className="error">{error}</p>}
        <button className="button" type="submit" disabled={isSubmitting}>
          {isSubmitting
            ? "Отправляем..."
            : mode === "login"
              ? "Войти"
              : "Зарегистрироваться"}
        </button>
      </form>

      <div className="auth-toggle">
        <span>
          {mode === "login"
            ? "Нет аккаунта?"
            : "Уже зарегистрированы?"}
        </span>
        <button className="link-button" onClick={toggleMode}>
          {mode === "login" ? "Перейти к регистрации" : "Перейти ко входу"}
        </button>
      </div>
    </div>
  );
};

