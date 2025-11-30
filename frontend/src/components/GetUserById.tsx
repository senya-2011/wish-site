import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { usersApi } from "../lib/api-client";

export const GetUserById = () => {
  const [userId, setUserId] = useState("");

  const mutation = useMutation({
    mutationFn: async (id: number) => {
      const response = await usersApi.getUserById(id);
      return response.data;
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!userId) return;
    const id = Number(userId);
    if (Number.isNaN(id)) {
      alert("Введите корректный ID пользователя");
      return;
    }
    mutation.mutate(id);
  };

  return (
    <div className="card">
      <h3>Получить пользователя по ID</h3>
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
        <button
          type="submit"
          className="button"
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "Загружаем..." : "Загрузить"}
        </button>
      </form>

      {mutation.isError && (
        <p className="error">Ошибка при загрузке пользователя</p>
      )}

      {mutation.isSuccess && (
        <pre className="response-box">
          {JSON.stringify(mutation.data, null, 2)}
        </pre>
      )}
    </div>
  );
};

