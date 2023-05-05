export type Routes =
	| 'Info'
	| 'Show'
	| 'Add'
	| 'Update'
	| 'RemoveById'
	| 'Clear'
	| 'ExecuteScript'
	| 'Exit'
	| 'RemoveFirst'
	| 'AddIfMax'
	| 'RemoveGreater'
	| 'MinByManufactureCost'
	| 'CountLessThanOwner'
	| 'FilterContainsName'
	| 'Login'
	| 'Register'
	| 'Refresh';

export interface User {
	id?: number;
	login: string;
	passwordHash: any; //bytearray
	refreshToken?: string;
}

export interface StrArg {
	str: string;
}
export interface ProductArg {
	product: Product;
}
export interface PersonArg {
	person: Person;
}
export interface UserArg {
	user: User;
}

export type Arg =
	| ({ type: 'StrArg' } & StrArg)
	| ({ type: 'ProductArg' } & ProductArg)
	| ({ type: 'PersonArg' } & PersonArg)
	| ({ type: 'UserArg' } & UserArg);

export interface Packet {
	type?: Routes;
	args: Arg[];
	headers: Record<string, Arg>;
	code: ResponseCode;
}

export type ResponseCode =
	| 'OK'
	| 'UNAUTHORIZED'
	| 'FORBIDDEN'
	| 'NOT_FOUND'
	| 'LOGIN_TIMEOUT'
	| 'IM_A_TEAPOT'
	| 'INTERNAL_ERROR';

// class TestException(val code: ResponseCode, e: Exception) :
//     Exception("${code.code} ($code) ${e.message}")

export interface Product {
	id: number;
	name: string;
	coordinates: Coordinates;
	creationDate: string;
	price: number;
	manufactureCost: number;
	unitOfMeasure?: UnitOfMeasure;
	owner: Person;
}

export interface Coordinates {
	x: number;
	y: number;
}

export interface Person {
	name: string;
	birthday?: '';
	nationality: Country;
}

export type UnitOfMeasure = 'SQUARE_METERS' | 'LITERS' | 'GRAMS';
export const UnitOfMeasureArr: UnitOfMeasure[] = ['SQUARE_METERS', 'LITERS', 'GRAMS'];

export type Country = 'CHINA' | 'SOUTH_KOREA' | 'JAPAN';
export const CountryArr: Country[] = ['CHINA', 'SOUTH_KOREA', 'JAPAN'];

export interface CmdInfo {
	name: string;
	help: string;
	args: Arg['type'][];
}
