import { useAuth } from "@/hooks/auth";
import { Button } from "@/components/ui/button";
import {User, LogOut, UserCircle } from "lucide-react";
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
import { Separator } from "@/components/ui/separator";

export default function NavBar() {
    const { user, logout } = useAuth();

    return (
        <nav className="sticky top-0 z-50 w-full bg-background">
            <div className="flex h-14 items-center px-4">
                <div className="flex items-center">
                    {/* <SidebarTrigger className="-ml-1" /> */}
                    <Separator orientation="vertical" className="mr-2 h-4" />
                    <Link href="/home" className="-ml-2">
                        <Image src="/logo.svg" alt="Logo" width={192} height={192} />
                    </Link>
                </div>
                <div className="flex-1" />
                <div className="flex items-center gap-2">
                    <DropdownMenu modal={false}>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="focus:outline-none">
                                <UserCircle className="scale-200" />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-36 mt-4 ">
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
            </div>
        </nav>
    );
}