import { addMessages, getLocaleFromNavigator, init } from 'svelte-i18n';

import en from './translations/en.json';
import es from './translations/es.json';
import fi from './translations/fi.json';
import ru from './translations/ru.json';
import sq from './translations/sq.json';

addMessages('en', en);
addMessages('es', es);
addMessages('fi', fi);
addMessages('ru', ru);
addMessages('sq', sq);

init({
	fallbackLocale: 'en',
	initialLocale: getLocaleFromNavigator()
});
