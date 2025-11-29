import { useQuery } from "@tanstack/react-query";
import { usersApi } from "../lib/api-client";

export const UserList = () => {
  const { data: users, isLoading, isError } = useQuery({
    queryKey: ["users"], 
    queryFn: async () => {
      const response = await usersApi.getAllUsers(); 
      return response.data; 
    },
  });

  if (isLoading) return <div>Загрузка юзеров...</div>;
  if (isError) return <div>Ошибка при загрузке!</div>;

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Список пользователей</h2>
      <ul className="space-y-2">
        {users?.map((user) => (
          <li key={user.user_id} className="border p-2 rounded shadow">
            {user.name} <span className="text-gray-500">({user.role})</span>
          </li>
        ))}
      </ul>
    </div>
  );
};