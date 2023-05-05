<script lang="ts">
	import Paper, { Title, Subtitle, Content } from '@smui/paper';
	import Button from '@smui/button';
	import axios from 'axios';
	import Str from './str.svelte';
	import Product from './product.svelte';
	import Person from './person.svelte';
	import type { Routes, CmdInfo, Arg, Packet } from '$lib/storage';
	import { onMount } from 'svelte';
	import Cookies from 'js-cookie';

	export let type: Routes;
	export let info: CmdInfo;

	let args: Arg[] = [];

	let output: string = '';

	const send = async () => {
		const data: Packet = {
			type: type || 'Show',
			args,
			headers: { authorization: { type: 'StrArg', str: Cookies.get('idc') as string } },
			code: 'OK'
		};

		let res = await axios.post('http://localhost:8080/cmd', data);
		console.log(res.data);
		output = res.data?.args?.[0]?.str || '';
	};
	onMount(async () => {
		await send();
	});
</script>

<Paper>
	<Title>{info.name}</Title>
	<Subtitle>{info.help}</Subtitle>
	<Content class="flex gap-4">
		{#each info.args as arg, idx}
			<div>
				{#if arg === 'StrArg'}
					<Str bind:value={args[idx]} />
				{:else if arg === 'ProductArg'}
					<Product bind:value={args[idx]} />
				{:else if arg === 'PersonArg'}
					<Person bind:value={args[idx]} />
				{/if}
			</div>
		{/each}
	</Content>

	<Button variant="raised" on:click={send}>send</Button>

	{#if output}
		<Paper>
			<Title>Output:</Title>
			<Content>
				{output}
			</Content>
		</Paper>
	{/if}
</Paper>
