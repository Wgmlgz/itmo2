<script lang="ts">
	import 'virtual:uno.css';
	import '../app.scss'

	import Drawer, { AppContent, Content, Header, Title, Subtitle, Scrim } from '@smui/drawer';
	import TopAppBar, { Row, Section, Title as TopBarTitle } from '@smui/top-app-bar';
	import IconButton from '@smui/icon-button';
	import Checkbox from '@smui/checkbox';
	import FormField from '@smui/form-field';
	// import LoremIpsum from '$lib/LoremIpsum.svelte';

	import Button, { Label } from '@smui/button';
	import List, { Item, Text, Graphic, Separator, Subheader } from '@smui/list';

	let open = false;
	let active = 'Inbox';

	let prominent = false;
	let dense = false;
	let secondaryColor = false;

	function setActive(value: string) {
		active = value;
		open = false;
	}
</script>

<Drawer variant="modal" bind:open>
	<Header>
		<Title>Super Mail</Title>
		<Subtitle>It's the best fake mail app drawer.</Subtitle>
	</Header>
	<Content>
		<List>
			<Item
				href="javascript:void(0)"
				on:click={() => setActive('Inbox')}
				activated={active === 'Inbox'}
			>
				<Graphic class="material-icons" aria-hidden="true">inbox</Graphic>
				<Text>Inbox</Text>
			</Item>
			<Item
				href="javascript:void(0)"
				on:click={() => setActive('Star')}
				activated={active === 'Star'}
			>
				<Graphic class="material-icons" aria-hidden="true">star</Graphic>
				<Text>Star</Text>
			</Item>
			<Item
				href="javascript:void(0)"
				on:click={() => setActive('Sent Mail')}
				activated={active === 'Sent Mail'}
			>
				<Graphic class="material-icons" aria-hidden="true">send</Graphic>
				<Text>Sent Mail</Text>
			</Item>
			<Item
				href="javascript:void(0)"
				on:click={() => setActive('Drafts')}
				activated={active === 'Drafts'}
			>
				<Graphic class="material-icons" aria-hidden="true">drafts</Graphic>
				<Text>Drafts</Text>
			</Item>

			<Separator />
			<Subheader tag="h6">Labels</Subheader>
			<Item
				href="javascript:void(0)"
				on:click={() => setActive('Family')}
				activated={active === 'Family'}
			>
				<Graphic class="material-icons" aria-hidden="true">bookmark</Graphic>
				<Text>Family</Text>
			</Item>
			<Item
				href="javascript:void(0)"
				on:click={() => setActive('Friends')}
				activated={active === 'Friends'}
			>
				<Graphic class="material-icons" aria-hidden="true">bookmark</Graphic>
				<Text>Friends</Text>
			</Item>
			<Item
				href="javascript:void(0)"
				on:click={() => setActive('Work')}
				activated={active === 'Work'}
			>
				<Graphic class="material-icons" aria-hidden="true">bookmark</Graphic>
				<Text>Work</Text>
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
				<Section align="end" toolbar>
					<IconButton class="material-icons" aria-label="Download">file_download</IconButton>
					<IconButton class="material-icons" aria-label="Print this page">print</IconButton>
					<IconButton class="material-icons" aria-label="Bookmark this page">bookmark</IconButton>
				</Section>
			</Row>
		</TopAppBar>
		<div class="flexor-content">
			<br />
			<pre class="status">Active: {active}</pre>
			<slot />
		</div>
	</main>
</AppContent>
