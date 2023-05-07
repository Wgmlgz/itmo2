import type {
  Arg,
  CmdInfo,
  Packet,
  Routes,
  TaggedPersonArg,
  TaggedProductArg,
  TaggedStrArg
} from './storage';
import axios from 'axios';
import Cookies from 'js-cookie';

export const send = async (type: Routes, args: Arg[]) => {
  const data: Packet = {
    type: type || 'Show',
    args,
    headers: { authorization: { type: 'StrArg', str: Cookies.get('idc') as string as string } },
    code: 'OK'
  };

  const res = await axios.post('http://localhost:8080/cmd', data);
  console.log(res.data);
  return res.data?.args?.[0]?.str || '';
};

export const commands: Partial<Record<Routes, CmdInfo>> = {
  Info: {
    name: 'Info',
    help: 'Info : output information about the collection (type, initialization date, number of items, etc.) to the standard output stream',
    args: []
  },
  Show: {
    name: 'Show',
    help: 'output to the standard output stream all the elements of the collection in a string representation',
    args: []
  },
  Add: {
    name: 'Add',
    help: '{element} : add a new item to the collection',
    args: ['ProductArg']
  },
  Update: {
    name: 'Update By Id',
    help: '{id} {element} : update the value of a collection item whose id is equal to the specified one',
    args: ['StrArg', 'ProductArg']
  },
  RemoveById: {
    name: 'Remove By Id',
    help: '{id} : delete an item from the collection by its id',
    args: ['StrArg']
  },
  Clear: {
    name: 'Clear',
    help: 'clear the collection',
    args: []
  },
  RemoveFirst: {
    name: 'Remove first',
    help: 'delete the first item from the collection',
    args: []
  },
  AddIfMax: {
    name: 'Add if max',
    help: '{element} : add a new item to the collection if its value exceeds the value of the largest item in this collection',
    args: ['ProductArg']
  },
  RemoveGreater: {
    name: 'Remove greater',
    help: '{element} : remove all items from the collection that exceed the specified',
    args: ['ProductArg']
  },
  MinByManufactureCost: {
    name: 'Min by manufacture cost',
    help: 'output any object from the collection whose value of the manufactureCost field is minimal',
    args: []
  },
  CountLessThanOwner: {
    name: 'Count less than owner',
    help: '{owner} : print the number of elements whose owner field value is less than the specified one',
    args: ['PersonArg']
  },
  FilterContainsName: {
    name: 'Filter contains name',
    help: '{name} : output elements whose name field value contains the specified substring',
    args: ['StrArg']
  }
};

export const defaultArg = (type: Arg['type']) =>
  structuredClone(
    {
      ProductArg: {
        type: 'ProductArg',
        product: {
          id: 0,
          name: '',
          coordinates: {
            x: 0,
            y: 0
          },
          creationDate: new Date().toISOString(),
          price: 0,
          manufactureCost: 0,
          unitOfMeasure: 'LITERS',
          owner: {
            name: '',
            nationality: 'CHINA',
            birthday: '2023-05-06T18:07'
          }
        }
      } satisfies TaggedProductArg,
      PersonArg: {
        type: 'PersonArg',
        person: {
          name: '',
          nationality: 'CHINA',
          birthday: ''
        }
      } satisfies TaggedPersonArg,
      StrArg: {
        type: 'StrArg',
        str: ''
      } satisfies TaggedStrArg,
      UserArg: {}
    }[type]
  ) as Arg;

function cyrb128(str: string) {
  let h1 = 1779033703,
    h2 = 3144134277,
    h3 = 1013904242,
    h4 = 2773480762;
  for (let i = 0, k; i < str.length; i++) {
    k = str.charCodeAt(i);
    h1 = h2 ^ Math.imul(h1 ^ k, 597399067);
    h2 = h3 ^ Math.imul(h2 ^ k, 2869860233);
    h3 = h4 ^ Math.imul(h3 ^ k, 951274213);
    h4 = h1 ^ Math.imul(h4 ^ k, 2716044179);
  }
  h1 = Math.imul(h3 ^ (h1 >>> 18), 597399067);
  h2 = Math.imul(h4 ^ (h2 >>> 22), 2869860233);
  h3 = Math.imul(h1 ^ (h3 >>> 17), 951274213);
  h4 = Math.imul(h2 ^ (h4 >>> 19), 2716044179);
  return [(h1 ^ h2 ^ h3 ^ h4) >>> 0, (h2 ^ h1) >>> 0, (h3 ^ h1) >>> 0, (h4 ^ h1) >>> 0];
}

export const nToColor = (n: number) => {
  const hash = cyrb128(String(n))[0];
  let color = '#';
  for (let i = 0; i < 3; i++) {
    const value = (hash >> (i * 8)) & 0xff;
    color += value.toString(16).substring(-2);
  }
  return color;
};
