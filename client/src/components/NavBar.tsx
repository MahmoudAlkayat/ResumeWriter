import { useState } from "react";
import { useAuth } from "@/hooks/auth";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { GraduationCap, Briefcase, User, Settings, Menu } from "lucide-react";
import Link from "next/link";
import Image from "next/image";

export default function NavBar() {
    const [isOpen, setIsOpen] = useState(false);
    const { user } = useAuth();
    const navigationItems = [
        { name: 'Education', href: '/education', icon: GraduationCap },
        { name: 'Career', href: '/career', icon: Briefcase },
        { name: 'Profile', href: '/profile', icon: User },
        { name: 'Settings', href: '/settings', icon: Settings },
    ];
    return (
        <nav className="shadow-lg px-4 h-14 flex items-center justify-between">
            <div className="flex items-center gap-4">
                <Sheet open={isOpen} onOpenChange={setIsOpen} modal={false}>
                    <SheetTrigger asChild>
                        <Button variant="ghost" className="p-2 rounded">
                            <Menu style={{ scale: 1.75 }} />
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
                <span className="text-sm font-medium">{user?.firstName} {user?.lastName}</span>
            </div>
        </nav>
    );
}