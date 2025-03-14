"use client";
import { createContext, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useToast } from "@/contexts/ToastProvider";
import { API_URL } from "@/lib/config";
import LoadingScreen from "@/components/LoadingScreen";

interface AuthContextType {
  isAuthenticated: boolean | null;
  isLogout: boolean;
  login: () => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const { showError, showSuccess } = useToast();
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
  const [isLogout, setIsLogout] = useState(false);
  const router = useRouter();

  const login = () => {
    setIsAuthenticated(true);
    setIsLogout(false);
  };

  const logout = async () => {
    try {
      const response = await fetch(`${API_URL}/auth/logout`, {
        method: "POST",
        credentials: "include"
      });
      if (!response.ok) {
        throw new Error("Failed to logout");
      } else {
        setIsLogout(true);
        setIsAuthenticated(false);
        if (!isLogout) {
          showSuccess("Successfully logged out");
        }
        router.push("/"); //Go to landing page on logout
      }
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      } else {
        showError("An unknown error occurred");
      }
    }
  };

  const checkAuth = async () => {
    try {
      const response = await fetch(`${API_URL}/auth/me`, {
        method: "GET",
        credentials: "include"
      });
      if (response.ok) {
        setIsAuthenticated(true);
        setIsLogout(false);
      } else {
        setIsAuthenticated(false);
      }
    } catch (error) {
      showError("Failed to check authentication");
      setIsAuthenticated(false);
    }
  };

  useEffect(() => {
    checkAuth();
  }, [])

  return (
    <AuthContext.Provider value={{ isAuthenticated, isLogout, login, logout }}>
      {isAuthenticated === null ? (
        <LoadingScreen />
      ) : children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
