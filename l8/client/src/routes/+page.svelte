<script lang="ts">
	import Select, { Option } from '@smui/select';
	import { onMount } from 'svelte';
	import axios from 'axios';
	import Cmd from '../lib/components/cmd.svelte';
	import type { Routes, CmdInfo } from '$lib/storage';
	let text = '';

	onMount(() => {
		const evtSource = new EventSource('http://localhost:8080/sse');
		evtSource.onmessage = (event) => {
			//console.log(event);
			text += event.data;
			console.log(event);
			var dataobj = JSON.parse(event.data);
		};
	});

	const commands: Partial<Record<Routes, CmdInfo>> = {
		Info: {
			name: 'info',
			help: 'info : output information about the collection (type, initialization date, number of items, etc.) to the standard output stream',
			args: []
		},
		Show: {
			name: 'show',
			help: 'show : output to the standard output stream all the elements of the collection in a string representation',
			args: []
		},
		Add: {
			name: 'add',
			help: 'add {element} : add a new item to the collection',
			args: ['ProductArg']
		},
		Update: {
			name: 'update $id',
			help: 'update id {element} : update the value of a collection item whose id is equal to the specified one',
			args: ['StrArg', 'ProductArg']
		},
		RemoveById: {
			name: 'remove_by_id $id',
			help: 'remove_by_id id : delete an item from the collection by its id',
			args: ['StrArg']
		},
		Clear: {
			name: 'clear',
			help: 'clear : clear the collection',
			args: []
		},
		RemoveFirst: {
			name: 'remove_first',
			help: 'remove_first : delete the first item from the collection',
			args: []
		},
		AddIfMax: {
			name: 'and_if_max',
			help: 'add_if_max {element} : add a new item to the collection if its value exceeds the value of the largest item in this collection',
			args: ['ProductArg']
		},
		RemoveGreater: {
			name: 'remove_greater',
			help: 'remove_greater {element} : remove all items from the collection that exceed the specified',
			args: ['ProductArg']
		},
		MinByManufactureCost: {
			name: 'min_by_manufacture_cost',
			help: 'min_by_manufacture_cost : output any object from the collection whose value of the manufactureCost field is minimal',
			args: []
		},
		CountLessThanOwner: {
			name: 'count_less_than_owner$item',
			help: 'count_less_than_owner owner : print the number of elements whose owner field value is less than the specified one',
			args: ['PersonArg']
		},
		FilterContainsName: {
			name: 'filter_contains_name $item',
			help: 'filter_contains_name name : output elements whose name field value contains the specified substring',
			args: ['StrArg']
		}
	};

	let selected: Routes;
</script>

<div>
	<Select bind:value={selected} variant="filled" label="sus">
		{#each Object.entries(commands) as [route, command]}
			<Option value={route}>{command.name}</Option>
		{/each}
	</Select>

	{#if selected}
		{@const cmd = commands[selected]}
		{#if cmd}
			<Cmd info={cmd} type={selected} />
		{/if}
	{/if}
</div>
