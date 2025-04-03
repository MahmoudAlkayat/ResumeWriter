"use client";
import { useAuth } from "@/hooks/auth";
import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import LoadingScreen from "@/components/LoadingScreen";
import { useToast } from "@/contexts/ToastProvider";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { Menu, GraduationCap, Briefcase, User, Settings } from "lucide-react";
import Image from "next/image";
import Link from "next/link";

export default function AuthenticatedLayout({ children }: { children: React.ReactNode }) {
    const { isAuthenticated } = useAuth();
    const router = useRouter();
    const isMounted = useRef(false);
    const { showInfo } = useToast();
    const [isOpen, setIsOpen] = useState(false);

    useEffect(() => {
        if (isAuthenticated === false && !isMounted.current) {
            router.replace('/login');
            showInfo("You must be logged in to view this page");
            isMounted.current = true;
        }
    }, [isAuthenticated, router, showInfo]);

    if (isAuthenticated === null || isAuthenticated === false) {
        return <LoadingScreen />;
    }

    const navigationItems = [
        { name: 'Education', href: '/education', icon: GraduationCap },
        { name: 'Career', href: '/career', icon: Briefcase },
        { name: 'Profile', href: '/profile', icon: User },
        { name: 'Settings', href: '/settings', icon: Settings },
    ];

    return (
        <div className="min-h-screen">
            {/* Navbar */}
            <nav className="shadow-lg px-4 h-14 flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <Sheet open={isOpen} onOpenChange={setIsOpen} modal={false}>
                        <SheetTrigger asChild>
                            <Button variant="ghost" className="p-2 rounded">
                                <Menu style={{ scale: 1.75 }}/>
                            </Button>
                        </SheetTrigger>
                        <SheetContent side="left" className="w-64 [&>button:last-child]:hidden shadow-lg">
                            <div className="flex flex-col gap-4 p-4 pt-8">
                                {navigationItems.map((item) => (
                                    <Link 
                                        key={item.name} 
                                        href={item.href}
                                        className="flex items-center gap-2 px-4 py-2 rounded-lg hover:bg-accent"
                                        onClick={() => setIsOpen(false)}
                                    >
                                        <item.icon className="h-5 w-5" />
                                        <span>{item.name}</span>
                                    </Link>
                                ))}
                            </div>
                        </SheetContent>
                    </Sheet>
                    <Link href="/home">
                        <Image src="/logo.svg" alt="Logo" width={192} height={192} />
                    </Link>
                </div>
                <div className="flex items-center gap-2">
                    <span className="text-sm font-medium">John Doe</span>
                </div>
            </nav>

            {/* Main Content */}
            <main className="">
                {children}
            </main>
        </div>
    );
}