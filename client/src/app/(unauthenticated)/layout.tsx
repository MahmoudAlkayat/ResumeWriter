"use client";
import { useAuth } from "@/hooks/auth";
import { useRouter } from "next/navigation";
import { useToast } from "@/contexts/ToastProvider";
import { useEffect } from "react";
import LoadingScreen from "@/components/LoadingScreen";

export default function UnauthenticatedLayout({ children }: { children: React.ReactNode }) {
    const { isAuthenticated } = useAuth();
    const router = useRouter();
    const { showInfo } = useToast();

    useEffect(() => {
        if (isAuthenticated === true) {
            router.replace('/home');
        }
    }, [isAuthenticated, router, showInfo]);

    if (isAuthenticated === null || isAuthenticated === true) {
        return <LoadingScreen />;
    }

    return <>{children}</>;
}