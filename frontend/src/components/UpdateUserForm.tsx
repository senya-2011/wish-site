import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { usersApi } from "../lib/api-client";
import type { UserUpdateRequest } from "../api";

export const UpdateUserForm = () => {
  const [userId, setUserId] = useState("");
  const [name, setName] = useState("");
  const [role, setRole] = useState<"member" | "admin" | "">("");
  const [password, setPassword] = useState("");

  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: async (payload: { id: number; body: UserUpdateRequest }) => {
      const response = await usersApi.updateUserById(payload.id, payload.body);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const id = Number(userId);
    if (Number.isNaN(id)) {
      alert("Введите корректный ID пользователя");
      return;
    }

    const body: UserUpdateRequest = {};
    if (name) body.name = name;
    if (role) body.role = role === "admin" ? "admin" : "member";
    if (password) body.password = password;

    if (Object.keys(body).length === 0) {
      alert("Укажите хотя бы одно поле для обновления");
      return;
    }

    mutation.mutate({ id, body });
  };

  return (
    <div className="card">
      <h3>Обновить пользователя</h3>
      <form onSubmit={handleSubmit} className="form">
        <input
          type="number"
          min={1}
          placeholder="ID пользователя"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          className="form-input"
          required
        />
        <select
          value={role}
          onChange={(e) => setRole(e.target.value as "member" | "admin" | "")}
          className="form-select"
        >
          <option value="">Роль (не менять)</option>
          <option value="member">member</option>
          <option value="admin">admin</option>
        </select>
        <input
          placeholder="Новое имя"
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="form-input"
        />
        <input
          type="password"
          placeholder="Новый пароль"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="form-input"
        />
        <button
          type="submit"
          className="button"
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "Сохраняем..." : "Обновить"}
        </button>
      </form>

      {mutation.isError && (
        <p className="error">Ошибка при обновлении пользователя</p>
      )}

      {mutation.isSuccess && (
        <pre className="response-box">
          {JSON.stringify(mutation.data, null, 2)}
        </pre>
      )}
    </div>
  );
};

