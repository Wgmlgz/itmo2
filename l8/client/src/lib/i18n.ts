import { addMessages, getLocaleFromNavigator, init } from 'svelte-i18n';

import en from './translations/en-US.json';
import es from './translations/es-MX.json';
import fi from './translations/fi.json';
import ru from './translations/ru-RU.json';
import sq from './translations/sq.json';

addMessages('en-US', en);
addMessages('es-MX', es);
addMessages('fi', fi);
addMessages('ru-RU', ru);
addMessages('sq', sq);

console.log(getLocaleFromNavigator())
init({
  fallbackLocale: 'en-US',
  initialLocale: getLocaleFromNavigator() || 'en-US'
});
