export * from './auth.service';
import { AuthService } from './auth.service';
export * from './groups.service';
import { GroupsService } from './groups.service';
export * from './users.service';
import { UsersService } from './users.service';
export const APIS = [AuthService, GroupsService, UsersService];
