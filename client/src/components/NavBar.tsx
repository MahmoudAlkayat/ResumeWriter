import { useState } from "react";
import { useAuth } from "@/hooks/auth";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { GraduationCap, Briefcase, User, Settings, Menu, LogOut, UserCircle2, HelpCircle, UserCircle, FileUser } from "lucide-react";
import Link from "next/link";
import Image from "next/image";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export default function NavBar() {
    const [isOpen, setIsOpen] = useState(false);
    const { user, logout } = useAuth();
    const navigationItems = [
        { name: 'Resume', href: '/resume', icon: FileUser },
        { name: 'Education', href: '/education', icon: GraduationCap },
        { name: 'Career', href: '/career', icon: Briefcase },
        // { name: 'Profile', href: '/profile', icon: User },
        // { name: 'Settings', href: '/settings', icon: Settings },
    ];
    return (
        <nav className="shadow-sm px-4 h-14 flex items-center justify-between sticky top-0 z-50 bg-background">
            <div className="flex items-center gap-4">
                <Sheet open={isOpen} onOpenChange={setIsOpen} modal={false}>
                    <SheetTrigger asChild>
                        <Button variant="ghost" className="p-2 rounded">
                            <Menu style={{ scale: 1.75 }} />
                        </Button>
                    </SheetTrigger>
                    <SheetContent 
                        side="left" 
                        className="w-64 [&>button:last-child]:hidden shadow-lg h-auto mt-14 bg-background"
                    >
                        <div className="flex flex-col gap-2 p-4">
                            {navigationItems.map((item) => (
                                <Link
                                    key={item.name}
                                    href={item.href}
                                    className="flex items-center gap-2 px-4 py-3 rounded-lg hover:bg-accent transition-colors"
                                    onClick={() => setIsOpen(false)}
                                >
                                    <item.icon className="h-5 w-5" />
                                    <span className="font-medium">{item.name}</span>
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
                <DropdownMenu modal={false}>
                    <DropdownMenuTrigger asChild>
                        <Button variant="ghost" className="flex items-center gap-2 p-4">
                            <UserCircle style={{ scale: 1.5 }}/>
                        </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" className="w-36 mt-4">
                        <DropdownMenuLabel>
                            <div className="flex flex-col items-center">
                                {user?.firstName} {user?.lastName}
                            <span className="text-muted-foreground">
                                {user?.email}
                            </span>
                            </div>
                        </DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem asChild>
                            <Link href="" className="cursor-pointer">
                                <User className="mr-2 h-4 w-4" />
                                <span>Profile</span>
                            </Link>
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem 
                            onClick={logout}
                            className="text-destructive focus:text-destructive cursor-pointer"
                        >
                            <LogOut className="mr-2 h-4 w-4" />
                            <span>Log out</span>
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>
        </nav>
    );
}