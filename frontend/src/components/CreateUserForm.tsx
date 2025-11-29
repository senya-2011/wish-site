import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { usersApi } from "../lib/api-client";
import type { UserRequest } from "../api"; // Импортируем сгенерированный DTO

export const CreateUserForm = () => {
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  
  const queryClient = useQueryClient();

  // Настраиваем мутацию
  const mutation = useMutation({
    mutationFn: (newUser: UserRequest) => {
      return usersApi.createUser(newUser);
    },
    onSuccess: () => {
      // Когда юзер создан, говорим React Query: "Список юзеров устарел, обнови его!"
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setName("");
      setPassword("");
      alert("Юзер создан!");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Вызываем мутацию
    mutation.mutate({ name, password });
  };

  return (
    <form onSubmit={handleSubmit} className="p-4 border mt-4">
      <h3 className="font-bold">Добавить юзера</h3>
      <input
        placeholder="Имя"
        value={name}
        onChange={(e) => setName(e.target.value)}
        className="border p-1 mr-2"
      />
      <input
        type="password"
        placeholder="Пароль"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        className="border p-1 mr-2"
      />
      <button 
        type="submit" 
        disabled={mutation.isPending}
        className="bg-blue-500 text-white p-1 rounded"
      >
        {mutation.isPending ? "Создаем..." : "Создать"}
      </button>
    </form>
  );
};