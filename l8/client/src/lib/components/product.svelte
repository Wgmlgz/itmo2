<script lang="ts">
	import { CountryArr, type Arg, UnitOfMeasureArr } from '$lib/storage';
	import Textfield from '@smui/textfield';
	import Select, { Option } from '@smui/select';
	import Paper from '@smui/paper';

	export let value: Arg;
	$: value = tmp;

	const tmp = {
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
			unitOfMeasure: undefined,
			owner: {
				name: '',
				nationality: 'CHINA',
				birthday: ''
			}
		}
	} satisfies Arg;
</script>

<div class="flex flex-col">
	<Textfield label="Name" bind:value={tmp.product.name} />
	<Paper variant="outlined">
		<p>Coordinates</p>
		<Textfield label="X" type="number" bind:value={tmp.product.coordinates.x} />
		<Textfield label="Y" type="number" bind:value={tmp.product.coordinates.y} />
	</Paper>

	<Textfield label="price" type="number" bind:value={tmp.product.price} />
	<Textfield label="manufactureCost" type="number" bind:value={tmp.product.manufactureCost} />

	<Select bind:value={tmp.product.unitOfMeasure} label="unitOfMeasure">
		{#each UnitOfMeasureArr as value}
			<Option {value}>{value}</Option>
		{/each}
	</Select>

	<Paper variant="outlined">
		<p>Owner</p>
		<Textfield label="Name" bind:value={tmp.product.owner.name} />
		<Textfield label="birthday" type="datetime-local" bind:value={tmp.product.owner.birthday} />

		<Select bind:value={tmp.product.owner.nationality} label="nationality">
			{#each CountryArr as value}
				<Option {value}>{value}</Option>
			{/each}
		</Select>
	</Paper>
</div>
