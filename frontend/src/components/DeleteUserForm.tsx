import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { usersApi } from "../lib/api-client";

export const DeleteUserForm = () => {
  const [userId, setUserId] = useState("");
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: async (id: number) => {
      await usersApi.deleteUserById(id);
      return id;
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
    mutation.mutate(id);
    setUserId("");
  };

  return (
    <div className="card">
      <h3>Удалить пользователя</h3>
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
          className="button danger"
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "Удаляем..." : "Удалить"}
        </button>
      </form>
      {mutation.isError && (
        <p className="error">Ошибка при удалении пользователя</p>
      )}
      {mutation.isSuccess && (
        <p className="notice">Пользователь удален</p>
      )}
    </div>
  );
};

