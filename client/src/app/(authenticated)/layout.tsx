"use client";
import { useAuth } from "@/hooks/auth";
import { useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import LoadingScreen from "@/components/LoadingScreen";
import { useToast } from "@/contexts/ToastProvider";
import NavBar from "@/components/NavBar";

export default function AuthenticatedLayout({ children }: { children: React.ReactNode }) {
    const { isAuthenticated, isLogout } = useAuth();
    const router = useRouter();
    const isMounted = useRef(false);
    const { showInfo } = useToast();

    useEffect(() => {
        if (isAuthenticated === false && !isMounted.current && !isLogout) {
            router.replace('/login');
            showInfo("You must be logged in to view this page");
            isMounted.current = true;
        }
    }, [isAuthenticated, router, showInfo, isLogout]);

    if (isAuthenticated === null || isAuthenticated === false) {
        return <LoadingScreen />;
    }

    return (
        <>
            <NavBar />
            <main>
                {children}
            </main>
        </>
    );
}