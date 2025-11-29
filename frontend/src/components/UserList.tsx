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

  if (isLoading) {
    return <div className="card text-muted">Загрузка пользователей...</div>;
  }

  if (isError) {
    return <div className="card error">Ошибка при загрузке списка</div>;
  }

  return (
    <div className="card">
      <h3>Список пользователей</h3>
      <ul className="simple-list">
        {users?.map((user) => (
          <li key={user.user_id}>
            <span>{user.name}</span>
            <span className="pill">{user.role}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};