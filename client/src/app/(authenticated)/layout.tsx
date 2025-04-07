"use client";
import { useAuth } from "@/hooks/auth";
import { useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import LoadingScreen from "@/components/LoadingScreen";
import { useToast } from "@/contexts/ToastProvider";
import NavBar from "@/components/NavBar";
import { SidebarProvider, SidebarInset } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { ThemeProvider } from "next-themes";

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
        return (
            <SidebarProvider className="flex flex-col" defaultOpen={false}>
                <NavBar />
                <div className="flex flex-1">
                    <AppSidebar />
                    <SidebarInset>
                        <LoadingScreen />
                    </SidebarInset>
                </div>
            </SidebarProvider>
        );
    }

    return (
        <ThemeProvider
        attribute="class"
        defaultTheme="system"
        enableSystem
        disableTransitionOnChange
      >
            <SidebarProvider className="flex flex-col" defaultOpen={false}>
                <NavBar />
                <div className="flex flex-1">
                    <AppSidebar />
                    <SidebarInset>
                        <main>
                            {children}
                        </main>
                    </SidebarInset>
                </div>
            </SidebarProvider>
        </ThemeProvider>
    );
}