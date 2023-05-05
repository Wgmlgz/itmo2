<script context="module">
	import { waitLocale } from 'svelte-i18n';

	export async function preload() {
		// awaits for the loading of the 'en-US' and 'en' dictionaries
		return waitLocale();
	}
</script>

<script lang="ts">
	import 'virtual:uno.css';
	import '../app.scss';

	import Drawer, { AppContent, Content, Header, Title, Subtitle, Scrim } from '@smui/drawer';
	import TopAppBar, { Row, Section, Title as TopBarTitle } from '@smui/top-app-bar';
	import IconButton from '@smui/icon-button';
	import Checkbox from '@smui/checkbox';
	import FormField from '@smui/form-field';
	import Select, { Option } from '@smui/select';
	import '$lib/i18n.ts';

	import { _, locale, locales } from 'svelte-i18n';

	import Button, { Label } from '@smui/button';
	import List, { Item, Text, Graphic, Separator, Subheader } from '@smui/list';
	import Auth from '../lib/components/auth.svelte';
	let value = 'Orange';
	let open = false;
	let active = 'Inbox';
	let helphelp = false;
	function setActive(value: string) {
		active = value;
		open = false;
	}
</script>

<svelte:head>
	<title>{$_('page_title')}</title>
</svelte:head>

<Drawer variant="modal" bind:open>
	<Header>
		<Title>Super Mail</Title>
		<Subtitle>It's the best fake mail app drawer.</Subtitle>
	</Header>
	<Content>
		<List>
			<Item
				href="javascript:void(0)"
				on:click={() => setActive('Star')}
				activated={active === 'Star'}
			>
				<Graphic class="material-icons" aria-hidden="true">star</Graphic>
				<Text>{$_('abobus')}</Text>
			</Item>
		</List>
	</Content>
</Drawer>

<Scrim />
<AppContent class="app-content">
	<main class="main-content">
		<TopAppBar variant="static" color="primary">
			<Row>
				<Section>
					<IconButton on:click={() => (open = !open)} class="material-icons">menu</IconButton>
					<TopBarTitle>Flex Static</TopBarTitle>
				</Section>
				<Section align="end" toolbar class="flex gap-3">
					<Select bind:value={$locale} variant="filled" label="sus">
						{#each $locales as locale}
							<Option value={locale}>{locale}</Option>
						{/each}
					</Select>
					<Auth />
				</Section>
			</Row>
		</TopAppBar>
		<div class="flexor-content">
			<br />
			<pre class="status">Active: {active}</pre>
			<a href="http://localhost:8080/login/oauth2/authorization/google">login login plspls</a>
			<a href="http://localhost:8080/logout">logout</a>

			<Checkbox bind:checked={helphelp} />
			<!-- {@html `<div>
				<div
					id="g_id_onload"
					data-client_id="912173031442-q6p845quib3kqgdh7el48ihpo0nm8dld.apps.googleusercontent.com"
					data-context="use"
					data-ux_mode="popup"
					data-auto_prompt="false"
				></div>
	
				<div
					class="g_id_signin"
					data-type="icon"
					data-shape="circle"
					data-theme="outline"
					data-text="continue_with"
					data-size="large"
				></div>
			</div>`} -->
			<slot />
		</div>
	</main>
</AppContent>
