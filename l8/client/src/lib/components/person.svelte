<script lang="ts">
	import { CountryArr, type Arg } from '$lib/storage';
	import Textfield from '@smui/textfield';
	import Select, { Option } from '@smui/select';
	import Paper from '@smui/paper';
	export let value: Arg;
	$: value = tmp;

	const tmp = {
		type: 'PersonArg',
		person: {
			name: '',
			nationality: 'CHINA',
			birthday: ''
		}
	} satisfies Arg;
</script>

<div>
	<Textfield label="Name" bind:value={tmp.person.name} />
	<Select bind:value={tmp.person.nationality} variant="filled" label="nationality">
		{#each CountryArr as value}
			<Option {value}>{value}</Option>
		{/each}
	</Select>
	<Textfield label="birthday" type="datetime-local" bind:value={tmp.person.birthday} />
</div>
