<script lang="ts">
	import type { Arg } from '$lib/storage';
	import Textfield from '@smui/textfield';

	export let value: Arg;
	$: value = tmp;

	const tmp = {
		type: 'StrArg',
		str: ''
	} satisfies Arg;
</script>

<Textfield bind:value={tmp.str} />
