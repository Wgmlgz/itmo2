<script lang="ts">
  import { CountryArr, type Arg, type PersonArg, type TaggedPersonArg } from '$lib/storage';
  import Textfield from '@smui/textfield';
  import Select, { Option } from '@smui/select';
  import Paper from '@smui/paper';
  import { _ } from 'svelte-i18n';


  export let value: TaggedPersonArg
</script>

<div>
  <Textfield label={$_("Name")} bind:value={value.person.name} />
  <Select bind:value={value.person.nationality} variant="filled" label={$_("nationality")}>
    {#each CountryArr as value}
      <Option {value}>{$_(value)}</Option>
    {/each}
  </Select>
  <Textfield label={$_("birthday")} type="datetime-local" bind:value={value.person.birthday} />
</div>
