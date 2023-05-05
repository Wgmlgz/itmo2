<script lang="ts">
	type Piece = 'p' | 'r' | 'b' | 'h' | 'k' | 'q';
	type Player = 'b' | 'w';
	type Cube = `${Player}${Piece}` | null;
	const N = 8;

	const board: Cube[][] = [
		[null, null, 'bp', 'bq', 'bk', 'bb', 'bh', 'br'],
		['br', 'bh', 'bb', 'bq', 'bk', 'bb', 'bh', 'br'],
		['bp', 'bp', 'bp', 'bp', 'bp', 'bp', 'bp', 'bp'],
		[null, null, null, null, null, null, null, null],
		[null, null, null, null, null, null, null, null],
		[null, null, null, null, null, null, null, null],
		[null, null, null, null, null, null, null, null],
		['wp', 'wp', 'wp', 'wp', 'wp', 'wp', 'wp', 'wp'],
		['wr', 'wh', 'wb', 'wq', 'wk', 'wb', 'wh', 'wr'],
		['wr', 'wh', 'wb', 'wq', 'wk', 'wp', null, null]
	];

	let handle: Cube = null;
	console.log(board);
</script>

<div class="board select-none flex flex-col">
	{#each board as row, i}
		<div class="row flex grow">
			{#each row as cube, j}
				<div
					on:pointerdown={(e) => {
						handle = cube;
						if (i !== 0 && i !== board.length - 1) cube = null;
					}}
					on:pointerup={(e) => {
						cube = handle;
					}}
					class="relative grid items-center justify-items-center field grow {i % 2 === j % 2
						? 'black'
						: 'white'}"
				>
					{#if cube !== null}
						<img alt={cube} draggable="false" class="absolute w-100% h-100%" src={`${cube}.png`} />
					{/if}
				</div>
			{/each}
		</div>
	{/each}
</div>

<style lang="scss">
	.board {
		aspect-ratio: 1 / 1;
		width: 100%;
		max-height: 100%;
	}
	.black {
		background: #006aff;
	}
	.white {
		background: #fffb00;
	}
</style>
