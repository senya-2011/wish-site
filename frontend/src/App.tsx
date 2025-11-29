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

function App() {
  return (
    <main className="app-shell">
      <header className="page-header">
        <h1>Wish site</h1>
        <p>Площадка для теста всех OpenAPI ручек core-service.</p>
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
