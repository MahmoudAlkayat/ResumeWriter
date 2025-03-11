"use client";
import { createContext, useContext } from "react";
import { toast } from "sonner";

interface ToastContextType {
  showSuccess: (message: string) => void;
  showInfo: (message: string) => void;
  showError: (message: string) => void;
}

const ToastContext = createContext<ToastContextType | null>(null);

export const ToastProvider = ({ children }: { children: React.ReactNode }) => {
  const showSuccess = (message: string) => {
    toast.success(message, {
      style: {
        background: "oklch(0.627 0.194 149.214)",
        color: "white",
        border: "1px solid oklch(0.627 0.194 149.214)",
        fontSize: "16px",
      },
    });
  };

  const showInfo = (message: string) => {
    toast.info(message, {
      style: {
        background: "oklch(0.901 0.058 230.902)",
        color: "black",
        border: "1px solid oklch(0.901 0.058 230.902)",
        fontSize: "16px",
      },
    });
  };

  const showError = (message: string) => {
    toast.error(message, {
      style: {
        background: "oklch(0.637 0.237 25.331)",
        color: "white",
        border: "1px solid oklch(0.637 0.237 25.331)",
        fontSize: "16px",
      },
    });
  };

  return (
    <ToastContext.Provider value={{ showSuccess, showInfo, showError }}>
      {children}
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return context;
};
