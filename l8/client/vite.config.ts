// vite.config.js
import { sveltekit } from '@sveltejs/kit/vite';
import UnoCSS from 'unocss/vite';
import extractorSvelte from '@unocss/extractor-svelte';

/** @type {import('vite').UserConfig} */
const config = {
	plugins: [
		UnoCSS({
			extractors: [extractorSvelte()]
			/* more options */
		}),
		sveltekit()
	]
};
export default config;
