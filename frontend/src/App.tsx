import "./App.css";
import { AuthPage } from "./components/AuthPage";
import { WishDashboard } from "./components/WishDashboard";
import { useAuth } from "./context/useAuth";

function App() {
  const { token, user, logout } = useAuth();

  if (!token) {
    return <AuthPage />;
  }

  return <WishDashboard user={user} onLogout={logout} />;
}

export default App;
