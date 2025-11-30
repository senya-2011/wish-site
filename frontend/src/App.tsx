import "./App.css";
import { UserList } from "./components/UserList";
import { CreateUserForm } from "./components/CreateUserForm";
import { GetUserById } from "./components/GetUserById";
import { UpdateUserForm } from "./components/UpdateUserForm";
import { DeleteUserForm } from "./components/DeleteUserForm";
import { GetUserWishes } from "./components/GetUserWishes";
import { CreateWishForm } from "./components/CreateWishForm";
import { GetWishById } from "./components/GetWishById";
import { UpdateWishForm } from "./components/UpdateWishForm";
import { DeleteWishForm } from "./components/DeleteWishForm";
import { AuthPage } from "./components/AuthPage";
import { useAuth } from "./context/AuthContext";

function App() {
  const { token, user, logout } = useAuth();

  if (!token) {
    return (
      <main className="app-shell">
        <AuthPage />
      </main>
    );
  }

  return (
    <main className="app-shell">
      <header className="page-header">
        <div>
          <h1>Wish site playground</h1>
          <p className="text-muted">
            Управляй пользователями и их желаниями прямо из браузера
          </p>
        </div>
        <div className="auth-meta">
          <span>Вошли как {user?.name ?? "неизвестно"}</span>
          <button className="button neutral" onClick={logout}>
            Выйти
          </button>
        </div>
      </header>

      <section className="section">
        <h2 className="section-title">Пользователи</h2>
        <div className="stack">
          <UserList />
          <CreateUserForm />
          <GetUserById />
          <UpdateUserForm />
          <DeleteUserForm />
        </div>
      </section>

      <section className="section">
        <h2 className="section-title">Желания</h2>
        <div className="stack">
          <GetUserWishes />
          <CreateWishForm />
          <GetWishById />
          <UpdateWishForm />
          <DeleteWishForm />
        </div>
      </section>
    </main>
  );
}

export default App;
