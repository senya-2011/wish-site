import "./App.css";
import { UserList } from "./components/UserList";
import { CreateUserForm } from "./components/CreateUserForm";

function App() {
  return (
    <main className="p-6 space-y-6">
      <header>
        <h1 className="text-3xl font-bold">Dabwish Admin</h1>
        <p className="text-gray-500">
          Управляй пользователями и их желаниями прямо из браузера
        </p>
      </header>

      <section className="bg-white rounded shadow">
        <UserList />
      </section>

      <section className="bg-white rounded shadow">
        <CreateUserForm />
      </section>
    </main>
  );
}

export default App;
