<script lang="ts">
	import type { Arg, TaggedStrArg } from '$lib/storage';
	import Textfield from '@smui/textfield';

	export let value: Arg;
	$: value = tmp;

	const tmp = {
		type: 'StrArg',
		str: ''
	} satisfies TaggedStrArg;
</script>

<Textfield bind:value={tmp.str} />
