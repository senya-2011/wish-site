import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { usersApi } from "../lib/api-client";
import type { UserRequest } from "../api"; 

export const CreateUserForm = () => {
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (newUser: UserRequest) => {
      return usersApi.createUser(newUser);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setName("");
      setPassword("");
      alert("Юзер создан!");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    mutation.mutate({ name, password });
  };

  return (
    <div className="card">
      <h3>Создать пользователя</h3>
      <form onSubmit={handleSubmit} className="form">
        <input
          placeholder="Имя"
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="form-input"
          required
        />
        <input
          type="password"
          placeholder="Пароль"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="form-input"
          required
        />
        <button
          type="submit"
          disabled={mutation.isPending}
          className="button"
        >
          {mutation.isPending ? "Создаем..." : "Создать"}
        </button>
      </form>
      {mutation.isError && (
        <p className="error">Не удалось создать пользователя</p>
      )}
      {mutation.isSuccess && (
        <p className="notice">Пользователь создан</p>
      )}
    </div>
  );
};